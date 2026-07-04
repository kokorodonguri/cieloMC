package io.cielomc.cielo.region;

import java.util.Objects;
import java.util.Optional;

public record RegionId(String worldKey, int regionX, int regionZ) {

    public RegionId {
        Objects.requireNonNull(worldKey, "worldKey");
    }

    public static RegionId fromChunk(final String worldKey, final int chunkX, final int chunkZ, final int regionSizeChunks) {
        return new RegionId(worldKey, Math.floorDiv(chunkX, regionSizeChunks), Math.floorDiv(chunkZ, regionSizeChunks));
    }

    public String asCompactString() {
        return this.worldKey + ":" + this.regionX + "," + this.regionZ;
    }

    public static Optional<RegionId> parseCompactString(final String input) {
        int separator = input.lastIndexOf(':');
        int comma = input.lastIndexOf(',');
        if (separator <= 0 || comma <= separator + 1 || comma == input.length() - 1) {
            return Optional.empty();
        }
        try {
            String worldKey = input.substring(0, separator);
            int regionX = Integer.parseInt(input.substring(separator + 1, comma));
            int regionZ = Integer.parseInt(input.substring(comma + 1));
            return Optional.of(new RegionId(worldKey, regionX, regionZ));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
