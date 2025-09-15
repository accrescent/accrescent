package app.accrescent.client.data

import android.os.LocaleList
import androidx.paging.PagingSource
import androidx.paging.PagingState
import build.buf.gen.accrescent.directory.v1.AppListing
import build.buf.gen.accrescent.directory.v1.DirectoryServiceGrpcKt
import build.buf.gen.accrescent.directory.v1.listAppListingsRequest
import io.grpc.StatusException

class AppListingPagingSource(
    private val directoryService: DirectoryServiceGrpcKt.DirectoryServiceCoroutineStub,
) : PagingSource<String, AppListing>() {
    override fun getRefreshKey(state: PagingState<String, AppListing>): String? {
        return null
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, AppListing> {
        return try {
            val request = listAppListingsRequest {
                pageSize = params.loadSize
                params.key?.let { pageToken = it }
                preferredLanguages.addAll(getPreferredLanguages())
            }
            val response = directoryService.listAppListings(request)

            LoadResult.Page(
                data = response.listingsList,
                prevKey = null,
                nextKey = if (response.hasNextPageToken()) response.nextPageToken else null,
            )
        } catch (e: StatusException) {
            LoadResult.Error(e)
        }
    }

    /**
     * Gets the preferred languages for app listings.
     *
     * @return the preferred languages in order of preference as BCP-47 language tags
     */
    private fun getPreferredLanguages(): List<String> {
        return LocaleList.getDefault().toLanguageTags().split(',')
    }
}