package ricardo.jean.galeriapublica;

import androidx.annotation.NonNull;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingSource;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

public class GalleryPagingSource extends ListenableFuturePagingSource<Integer, ImageData> {

    GalleryRepository galleryRepository;
    Integer initialLoadSize = 0;
    public GalleryPagingSource(GalleryRepository galleryRepository) {
        this.galleryRepository = galleryRepository;
    }



    @Nullable
    @Override
    public ListenableFuture<PagingSource.LoadResult<Integer, ImageData>> loadFuture(@NonNull PagingSource.LoadParams<Integer> loadParams) {
        Integer nextPageNumber = loadParams.getKey();
        if (nextPageNumber == null) {
            nextPageNumber = 1;
            initialLoadSize = loadParams.getLoadSize();
        }
        Integer offSet = 0;
        if(nextPageNumber == 2) {
            offSet = initialLoadSize;
        }
        else {
            offSet = ((nextPageNumber - 1) * loadParams.getLoadSize()) + (initialLoadSize - loadParams.getLoadSize());
        }

        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

        Integer finalOffSet = offSet;
        Integer finalNextPageNumber = nextPageNumber;

        ListenableFuture<PagingSource.LoadResult<Integer, ImageData>> lf = service.submit(new Callable<PagingSource.LoadResult<Integer, ImageData>>() {
            @Override
            public PagingSource.LoadResult<Integer, ImageData> call() {
                List<ImageData> imageDataList = null;
                try {
                    imageDataList = galleryRepository.loadImageData(loadParams.getLoadSize(), finalOffSet);
                    Integer nextKey = null;
                    if(imageDataList.size() >= loadParams.getLoadSize()) {
                        nextKey = finalNextPageNumber + 1;
                    }
                    return new PagingSource.LoadResult.Page<Integer, ImageData>(imageDataList, null, nextKey);
                } catch (FileNotFoundException e) {
                    return new PagingSource.LoadResult.Error<>(e);
                }
            }
        });
        return lf;
    }

    @androidx.annotation.Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, ImageData> pagingState) {
        return null;
    }
}
