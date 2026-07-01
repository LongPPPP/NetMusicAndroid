package com.example.netmusicandroid.adapter

import com.example.netmusicandroid.data.model.SongItem
import org.junit.Assert.assertEquals
import org.junit.Test

class SongListAdapterPositionRefreshTest {
    @Test
    fun firstChangedIndex_returnsDeletedPositionWhenMiddleSongRemoved() {
        val oldList = listOf(
            song(1),
            song(2),
            song(3),
            song(4)
        )
        val newList = listOf(
            song(1),
            song(3),
            song(4)
        )

        val firstChangedIndex = SongListAdapter.firstChangedIndexForPositionLabels(oldList, newList)

        assertEquals(1, firstChangedIndex)
    }

    @Test
    fun firstChangedIndex_returnsLastVisiblePositionWhenLastSongRemoved() {
        val oldList = listOf(song(1), song(2), song(3))
        val newList = listOf(song(1), song(2))

        val firstChangedIndex = SongListAdapter.firstChangedIndexForPositionLabels(oldList, newList)

        assertEquals(2, firstChangedIndex)
    }

    @Test
    fun firstChangedIndex_returnsMinusOneWhenOrderAndSizeAreUnchanged() {
        val oldList = listOf(song(1), song(2), song(3))
        val newList = listOf(song(1), song(2), song(3))

        val firstChangedIndex = SongListAdapter.firstChangedIndexForPositionLabels(oldList, newList)

        assertEquals(-1, firstChangedIndex)
    }

    private fun song(id: Int) = SongItem(
        song_id = id,
        song_name = "Song $id",
        singer_id = id,
        singer_name = "Singer $id",
        cover_url = "cover-$id.jpg",
        play_url = "song-$id.mp3",
        duration = 180,
        added_at = ""
    )
}
