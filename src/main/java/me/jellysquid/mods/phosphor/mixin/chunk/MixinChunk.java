package me.jellysquid.mods.phosphor.mixin.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mixin(Chunk.class)
public abstract class MixinChunk {
    @Shadow
    @Final
    private ChunkPos pos;

    @Shadow
    @Final
    private ChunkSection[] sections;

    @Shadow
    @Final
    private World world;

    /**
     * This implementation avoids iterating over empty chunk sections and uses direct access to read out block states
     * instead. Instead of allocating a BlockPos for every block in the chunk, they're now only allocated once we find
     * a light source.
     *
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    public Stream<BlockPos> getLightSources() {
        List<BlockPos> list = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int startX = this.pos.getXStart();
        int startZ = this.pos.getZStart();

        ChunkSection[] chunkSections = this.sections;

        for (ChunkSection section : chunkSections) {
            if (section == null || section.isEmpty()) {
                continue;
            }

            int startY = section.getYLocation();

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        pos.setPos(startX + x, startY + y, startZ + z);

                        if (state.getLightValue(this.world, pos) != 0) {
                            list.add(pos.toImmutable());
                        }
                    }
                }
            }
        }

        if (list.isEmpty()) {
            return Stream.empty();
        }

        return list.stream();
    }
}
