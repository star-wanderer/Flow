package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    val item = state.firstItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getAfter(item.id, state.config.pageSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
                LoadType.APPEND -> {
                    val item = state.lastItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getBefore(item.id,state.config.pageSize)
                }
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

        db.withTransaction {
            when(loadType){
                LoadType.REFRESH-> {
                    postRemoteKeyDao.insert(
                        PostRemoteKeyEntity(
                            type = PostRemoteKeyEntity.KeyType.AFTER,
                            id = body.first().id
                        )
                    )
                }
                LoadType.PREPEND->{
                    postRemoteKeyDao.insert(
                        PostRemoteKeyEntity(
                            type = PostRemoteKeyEntity.KeyType.AFTER,
                            id = body.first().id
                        )
                    )
                }
                LoadType.APPEND->{
                    postRemoteKeyDao.insert(
                        PostRemoteKeyEntity(
                            type = PostRemoteKeyEntity.KeyType.BEFORE,
                            id = body.last().id
                        )
                    )
                }
            }
        }
            postDao.insert(body.toEntity())
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())

        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}