package com.vomiter.survivorsdelight.data.book;

import com.vomiter.survivorsdelight.data.book.builder.EntryJson;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SDPatchouliEntryProvider implements DataProvider {
    private final PackOutput output;
    private final List<EntryJson> entries = new ArrayList<>();
    private String LANG = "en_us";
    public void setLang(String lang){
        this.LANG = lang;
    }

    public String getLANG() {
        return LANG;
    }


    public SDPatchouliEntryProvider(PackOutput output) { this.output = output; }
    public void entry(EntryJson entry) { entries.add(entry); }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (EntryJson e : entries) {
            Path file = output.getOutputFolder().resolve(String.join("/",
                    "assets",
                    SDPatchouliConstants.MODID,
                    SDPatchouliConstants.bookFolderRL().getPath(),
                    LANG,
                    "entries",
                    e.getId() + ".json"
            ));
            futures.add(DataProvider.saveStable(cache, e.toJson(), file));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() { return "Patchouli Book: entries"; }
}
