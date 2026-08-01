package com.example.data

import android.content.Context
import androidx.room.*
import com.example.dsp.model.EffectParameter
import com.example.dsp.model.EffectType
import com.example.dsp.model.EffectUnit
import com.example.dsp.model.PresetData
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val effectsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

class Converters {
    companion object {
        fun effectsToJson(effects: List<EffectUnit>): String {
            val jsonArray = JSONArray()
            for (unit in effects) {
                val obj = JSONObject()
                obj.put("id", unit.id)
                obj.put("type", unit.type.name)
                obj.put("name", unit.name)
                obj.put("enabled", unit.enabled)
                obj.put("colorHex", unit.colorHex)

                val paramsArray = JSONArray()
                for (p in unit.parameters) {
                    val pObj = JSONObject()
                    pObj.put("key", p.key)
                    pObj.put("label", p.label)
                    pObj.put("value", p.value.toDouble())
                    pObj.put("minValue", p.minValue.toDouble())
                    pObj.put("maxValue", p.maxValue.toDouble())
                    pObj.put("unit", p.unit)
                    paramsArray.put(pObj)
                }
                obj.put("parameters", paramsArray)
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }

        fun jsonToEffects(jsonStr: String): List<EffectUnit> {
            val list = mutableListOf<EffectUnit>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val typeName = obj.getString("type")
                    val type = try { EffectType.valueOf(typeName) } catch (e: Exception) { EffectType.OVERDRIVE }
                    val name = obj.optString("name", type.displayName)
                    val enabled = obj.optBoolean("enabled", true)
                    val colorHex = obj.optLong("colorHex", type.defaultColor)

                    val paramsList = mutableListOf<EffectParameter>()
                    if (obj.has("parameters")) {
                        val pArray = obj.getJSONArray("parameters")
                        for (j in 0 until pArray.length()) {
                            val pObj = pArray.getJSONObject(j)
                            paramsList.add(
                                EffectParameter(
                                    key = pObj.getString("key"),
                                    label = pObj.getString("label"),
                                    value = pObj.getDouble("value").toFloat(),
                                    minValue = pObj.getDouble("minValue").toFloat(),
                                    maxValue = pObj.getDouble("maxValue").toFloat(),
                                    unit = pObj.optString("unit", "")
                                )
                            )
                        }
                    }
                    list.add(EffectUnit(id, type, name, enabled, paramsList, colorHex))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY timestamp DESC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity): Long

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)
}

@Database(entities = [PresetEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dsp4guitar_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class PresetRepository(private val presetDao: PresetDao) {
    val allPresets: Flow<List<PresetEntity>> = presetDao.getAllPresets()

    suspend fun savePreset(preset: PresetData): Long {
        val entity = PresetEntity(
            id = preset.id,
            title = preset.title,
            category = preset.category,
            description = preset.description,
            effectsJson = Converters.effectsToJson(preset.effects)
        )
        return presetDao.insertPreset(entity)
    }

    suspend fun deletePreset(id: Long) {
        presetDao.deletePresetById(id)
    }
}
