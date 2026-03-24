package com.vomiter.survivorsdelight.data.book;

import com.vomiter.survivorsdelight.data.book.builder.CategoryJson;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SDPatchouliCategoryProvider implements DataProvider {
    private final PackOutput output;
    private final List<CategoryJson> categories = new ArrayList<>();
    private String LANG = "en_us";
    public void setLang(String lang){
        this.LANG = lang;
    }

    public String getLANG() {
        return LANG;
    }

    public SDPatchouliCategoryProvider(PackOutput output) { this.output = output; }
    public void category(CategoryJson category) { categories.add(category); }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (CategoryJson cat : categories) {
            Path file = output.getOutputFolder().resolve(String.join("/",
                    "assets",
                    SDPatchouliConstants.MODID,
                    SDPatchouliConstants.bookFolderRL().getPath(),
                    LANG,
                    "categories",
                    cat.id() + ".json"
            ));
            futures.add(DataProvider.saveStable(cache, cat.toJson(), file));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() { return "Patchouli Book: categories"; }
}

