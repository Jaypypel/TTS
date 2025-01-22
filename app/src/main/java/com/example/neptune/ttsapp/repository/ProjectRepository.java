//package com.example.neptune.ttsapp.repository;
//
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.example.neptune.ttsapp.AppExecutors;
//import com.example.neptune.ttsapp.Network.ProjectServiceInterface;
//import com.example.neptune.ttsapp.Resource;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import retrofit2.Response;
//
//@Singleton
//public class ProjectRepository {
//    private final ProjectServiceInterface projectService;
//    private final AppExecutors appExecutors;
//
//    @Inject
//    public ProjectRepository(ProjectServiceInterface projectService, AppExecutors appExecutors) {
//        this.projectService = projectService;
//        this.appExecutors = appExecutors;
//    }
//
//
//    public LiveData<Resource<ArrayList<String>>> getProjectNames(){
//        MutableLiveData<Resource<ArrayList<String>>> result = new MutableLiveData<>();
//        result.postValue(Resource.loading(null));
//        appExecutors.getNetworkIO().execute(() -> {
//            try{
//                Response<ArrayList<String>> response = projectService.getProjectNameList();
//                if(response.isSuccessful() && response.body() != null){
//                    result.postValue(Resource.success(response.body()));
//                }else {
//                    result.postValue(Resource.error("Failed to fetch",null));
//
//                }
//            }catch (IOException e){
//                result.postValue(Resource.error("Network error",null));
//            }
//        });
//        return result;
//    }
//
//    public LiveData<Resource<String>> getProjectCodeForProjectName(String projectName){
//        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
//        result.postValue(Resource.loading(null));
//        appExecutors.getNetworkIO().execute(() ->{
//            try {
//                Response<String> response = projectService.getProjectCodeViaProjectName(projectName);
//                if(response.isSuccessful() && response.body() != null){
//                    result.postValue(Resource.success(response.body()));
//                }else {
//                    result.postValue(Resource.error("Failed to fetch the project code",null));
//
//                }
//            }catch (IOException e){
//                result.postValue(Resource.error("Network error",null));
//            }
//        });
//        return result;
//    }
//}
