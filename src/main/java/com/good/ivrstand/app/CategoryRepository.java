package com.good.ivrstand.app;

import com.good.ivrstand.domain.Category;
import com.good.ivrstand.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findById(long id);
    void deleteById(long id);

    Category findByTitleIgnoreCase(String title);

    @Query("SELECT c FROM Category c WHERE FUNCTION('levenshtein', LOWER(c.title), LOWER(:title)) < 3")
    Page<Category> findByNearTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE LOWER(c.title) = LOWER(:title)")
    Page<Category> findByExactTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE FUNCTION('levenshtein', LOWER(c.title), LOWER(:title)) < 5 OR LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Category> findByTitleWithTypos(@Param("title") String title, Pageable pageable);

    @Query("SELECT с FROM Category с WHERE (FUNCTION('levenshtein', LOWER(с.title), LOWER(:title)) < 5 OR LOWER(с.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND (LOWER(с.title) LIKE CONCAT('%', :num, '%'))")
    Page<Category> findByTitleAndNumber(@Param("title") String title, @Param("num") int num, Pageable pageable);

    default List<Integer> findNumbers(String title) {
        List<Integer> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(title);

        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }

        return numbers;
    }

    default Page<Category> findByTitle(String title, Pageable pageable) {
        Page<Category> categories = null;
        List<Integer> numbers = findNumbers(title);
        if (!numbers.isEmpty()) {
            for (int num : numbers) {
                Page<Category> foundCategories = findByTitleAndNumber(title, num, pageable);
                if (categories == null) {
                    categories = foundCategories;
                } else {
                    categories = mergePages(categories, foundCategories);
                }
            }
        }  else {
            categories = findByExactTitle(title, pageable);
        }
        if (categories.isEmpty())
            categories = findByNearTitle(title, pageable);
        if (categories.isEmpty())
            categories = findByTitleWithTypos(title, pageable);
        return categories;
    }

    private Page<Category> mergePages(Page<Category> page1, Page<Category> page2) {
        Set<Category> mergedSet = new HashSet<>(page1.getContent());
        List<Category> mergedContent = new ArrayList<>(page1.getContent());
        for (Category category : page2.getContent()) {
            if (!mergedSet.contains(category)) {
                mergedContent.add(category);
            }
        }
        if (!mergedContent.isEmpty()) {
            return new PageImpl<>(mergedContent, PageRequest.of(0, mergedContent.size()), mergedContent.size());
        } else {
            return Page.empty();
        }
    }

    @Query("SELECT c FROM Category c WHERE SIZE(c.childrenCategories) = 0 AND c.parentCategory IS NULL")
    Page<Category> findUnallocatedCategories(Pageable pageable);
}
