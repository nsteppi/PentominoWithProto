export const grid_cell_to_coordinates = (col, row, pento_block_size) => {
    return [col * pento_block_size, row * pento_block_size];
};

export const coordinates_to_grid_cell = (x, y, pento_block_size) => {
    return [Math.floor(x/pento_block_size), Math.floor(y/pento_block_size)];
};
