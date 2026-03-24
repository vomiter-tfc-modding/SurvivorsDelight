package com.vomiter.survivorsdelight.data.book;

import com.google.gson.JsonObject;
import com.vomiter.survivorsdelight.data.book.builder.BookJson;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SDPatchouliBookProvider implements DataProvider {
    private final PackOutput output;
    private final List<BookJson> books = new ArrayList<>();

    public SDPatchouliBookProvider(PackOutput output) { this.output = output; }
    public void book(BookJson book) { this.books.add(book); }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        if (books.isEmpty()) return CompletableFuture.completedFuture(null);
        Path root = output.getOutputFolder()
                .resolve("assets/" + SDPatchouliConstants.MODID + "/" + SDPatchouliConstants.bookFolderRL().getPath());
        Path file = root.resolve("book.json");
        JsonObject merged = books.get(0).toJson();
        return DataProvider.saveStable(cache, merged, file);
    }

    @Override
    public String getName() { return "Patchouli Book: book.json"; }
}