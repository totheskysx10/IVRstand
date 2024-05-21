package com.good.ivrstand.app;

import java.util.List;

public interface FlaskApiVectorSearchService {
    List<String> getEmbeddings(List<String> dialog);
    void addTitle(String title);
    void deleteTitle(String title);
}
