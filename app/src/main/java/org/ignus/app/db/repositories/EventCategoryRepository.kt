package org.ignus.app.db.repositories

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.ignus.app.App
import org.ignus.app.db.models.EventCategory
import org.ignus.app.db.room.EventCategoryDao
import org.ignus.app.db.room.db
import org.ignus.app.services.APIClient
import org.ignus.app.services.APIService
import java.util.concurrent.TimeUnit

class EventCategoryRepository {

    val loading: MutableLiveData<Boolean> = MutableLiveData()
    private val eventCategoryDao: EventCategoryDao by lazy { db.eventCategoryDao() }

    fun getEventCategories(): Observable<List<EventCategory>> {
        refreshEventCategories()
        return eventCategoryDao.getAll()
    }

    private fun refreshEventCategories(): Disposable? {

        loading.postValue(true)

        return APIClient.getClient().create(APIService::class.java)
            .getEventCategories()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .debounce(400, TimeUnit.MILLISECONDS)
            .subscribe({
                Log.d("suthar", "subscribe ${it.size}")
                eventCategoryDao.deleteAll()
                eventCategoryDao.saveAll(it)
                loading.postValue(false)
            }, {
                loading.postValue(false)
                Log.d("suthar", "Error:  $it")
                Toast.makeText(App.instance, "Something went wrong!", Toast.LENGTH_SHORT).show()
            })
    }
}