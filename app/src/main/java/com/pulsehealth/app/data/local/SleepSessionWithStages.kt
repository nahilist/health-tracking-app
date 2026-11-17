package com.pulsehealth.app.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class SleepSessionWithStages(
    @Embedded val session: SleepSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val stages: List<SleepStageEntity>
)
