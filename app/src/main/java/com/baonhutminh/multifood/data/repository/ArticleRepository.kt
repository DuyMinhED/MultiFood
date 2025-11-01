package com.baonhutminh.multifood.data.repository

import com.baonhutminh.multifood.R
import com.baonhutminh.multifood.data.model.Article
import kotlinx.coroutines.delay

class ArticleRepository {

    suspend fun getArtilce():List<Article>{

        delay(3000)

        return listOf(
            Article(1, "Title 1", "Content 1", R.drawable.ut6, 4.5f),
            Article(2, "Title 2", "Content 2", R.drawable.img, 3.5f),
            Article(3, "Title 3", "Content 3", R.drawable.img, 2.5f)
        )

    }


    suspend fun getArticleById(): Article {
        return Article(1, "Title 1", "Content 1", R.drawable.ut6, 4.5f)
    }

}