package ru.netology.nmedia.repository

import androidx.lifecycle.*
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override var data = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostsApi.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long): Boolean {
        var err = false
        try {
            dao.removeById(id)

            val response = PostsApi.service.removeById(id)
            if (!response.isSuccessful) {
                err = true
//                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            err = true
//            throw NetworkError
        } catch (e: Exception) {
            err = true
//            throw UnknownError
        }
        return err
    }

    override suspend fun likeById(post: Post): Boolean {
        val response: retrofit2.Response<Post>
        val needToLike = !post.likedByMe
        var err = false
        try {
            if(needToLike) post.likes += 1
            else post.likes -= 1
            dao.insert(PostEntity.fromDto(post))
            data = dao.getAll().map(List<PostEntity>::toDto)

            response = if (needToLike) PostsApi.service.likeById(post.id)
            else PostsApi.service.dislikeById(post.id)
            if (!response.isSuccessful) {
                err = true
//                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            err = true
//            throw NetworkError
        } catch (e: Exception) {
            err = true
//            throw UnknownError
        }
        return err
    }
}
