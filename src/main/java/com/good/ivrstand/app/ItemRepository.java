package com.good.ivrstand.app;

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
public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findById(long id);
    void deleteById(long id);

    Item findByTitleIgnoreCase(String title);

    @Query("SELECT i FROM Item i WHERE LOWER(i.title) LIKE CONCAT('%', LOWER(:title), '%') OR LOWER(i.title) LIKE CONCAT(SUBSTRING(:title, 1, LENGTH(:title) - 1), '%')")
    Page<Item> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE LOWER(i.title) = LOWER(:title)")
    Page<Item> findByExactTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE FUNCTION('levenshtein', LOWER(i.title), LOWER(:title)) < 3")
    Page<Item> findByNearTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE FUNCTION('levenshtein', LOWER(i.title), LOWER(:title)) < 5 OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Item> findByTitleWithTypos(@Param("title") String title, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE (FUNCTION('levenshtein', LOWER(i.title), LOWER(:title)) < 5 OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND (LOWER(i.title) LIKE CONCAT('%', :num, '%'))")
    Page<Item> findByTitleAndNumber(@Param("title") String title, @Param("num") int num, Pageable pageable);

    default List<Integer> findNumbers(String title) {
        List<Integer> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(title);

        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }

        return numbers;
    }

    default Page<Item> findByTitle(String title, Pageable pageable) {
        Page<Item> items = null;
        List<Integer> numbers = findNumbers(title);
        if (!numbers.isEmpty()) {
            for (int num : numbers) {
                Page<Item> foundItems = findByTitleAndNumber(title, num, pageable);
                if (items == null) {
                    items = foundItems;
                } else {
                    items = mergePages(items, foundItems);
                }
            }
        } else {
            items = findByExactTitle(title, pageable);
        }
        if (items.isEmpty())
            items = findByNearTitle(title, pageable);
        if (items.isEmpty())
            items = findByTitleWithTypos(title, pageable);
        return items;
    }

    @Query("SELECT i FROM Item i WHERE i.category.id = :categoryId AND FUNCTION('levenshtein', LOWER(i.title), LOWER(:title)) < 3")
    Page<Item> findByNearTitleAndCategoryId(@Param("title") String title, @Param("categoryId") long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.category.id = :categoryId AND LOWER(i.title) = LOWER(:title)")
    Page<Item> findByExactTitleAndCategoryId(@Param("title") String title, @Param("categoryId") long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE (i.category.id = :categoryId) AND (FUNCTION('levenshtein', LOWER(i.title), LOWER(:title)) < 5 OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%')))")
    Page<Item> findByTitleAndCategoryIdWithTypos(@Param("title") String title, @Param("categoryId") long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE (i.category.id = :categoryId) AND (FUNCTION('levenshtein', LOWER(i.title), LOWER(:title)) < 5 OR LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND (LOWER(i.title) LIKE CONCAT('%', :num, '%'))")
    Page<Item> findByTitleAndCategoryIdWithTyposAndNumbers(@Param("title") String title, @Param("categoryId") long categoryId, @Param("num") int num, Pageable pageable);

    default Page<Item> findByTitleAndCategoryId(long categoryId, String title, Pageable pageable) {
        Page<Item> items = null;
        List<Integer> numbers = findNumbers(title);
        if (!numbers.isEmpty()) {
            for (int num : numbers) {
                Page<Item> foundItems = findByTitleAndCategoryIdWithTyposAndNumbers(title, categoryId, num, pageable);
                if (items == null) {
                    items = foundItems;
                } else {
                    items = mergePages(items, foundItems);
                }
            }
        }  else {
            items = findByExactTitleAndCategoryId(title, categoryId, pageable);
        }
        if (items.isEmpty())
            items = findByNearTitleAndCategoryId(title, categoryId, pageable);
        if (items.isEmpty())
            items = findByTitleAndCategoryIdWithTypos(title, categoryId, pageable);
        return items;
    }

    private Page<Item> mergePages(Page<Item> page1, Page<Item> page2) {
        Set<Item> mergedSet = new HashSet<>(page1.getContent());
        List<Item> mergedContent = new ArrayList<>(page1.getContent());
        for (Item item : page2.getContent()) {
            if (!mergedSet.contains(item)) {
                mergedContent.add(item);
            }
        }
        if (!mergedContent.isEmpty()) {
            return new PageImpl<>(mergedContent, PageRequest.of(0, mergedContent.size()), mergedContent.size());
        } else {
            return Page.empty();
        }
    }

    Page<Item> findByCategoryId(long categoryId, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.category IS NULL")
    Page<Item> findItemsWithNullCategory(Pageable pageable);

    Page<Item> findByKeyWordIgnoreCase(String word, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE LOWER(i.keyWord) LIKE CONCAT('%', LOWER(:word), '%') OR LOWER(i.keyWord) LIKE CONCAT(SUBSTRING(:word, 1, LENGTH(:word) - 1), '%')")
    Page<Item> findByKeyWordContainingIgnoreCase(String word, Pageable pageable);
}