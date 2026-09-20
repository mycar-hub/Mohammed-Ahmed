package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.*
import java.util.Date

class Converters {
  @TypeConverter
  fun fromTimestamp(value: Long?): Date? {
    return value?.let { Date(it) }
  }

  @TypeConverter
  fun dateToTimestamp(date: Date?): Long? {
    return date?.time
  }

  @TypeConverter
  fun fromStringList(value: String?): List<String> {
    if (value.isNullOrEmpty()) return emptyList()
    return value.split("|||")
  }

  @TypeConverter
  fun toStringList(list: List<String>?): String {
    if (list.isNullOrEmpty()) return ""
    return list.joinToString("|||")
  }
}
