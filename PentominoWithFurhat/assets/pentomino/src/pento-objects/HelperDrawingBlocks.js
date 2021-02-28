export const draw_border = (ctx, block, offsetX, offsetY) => {

    var start_x = block.get_x() + offsetX;
    var start_y = block.get_y() + offsetY;

    for (var row = 1; row <= block.get_vertices().length; row++) {
        ctx.beginPath();
        ctx.moveTo(start_x, start_y);
        var row_index = row < block.get_vertices().length ? row: 0;


        ctx.lineWidth = block.get_edge_style(row_index);
        ctx.strokeStyle = block.get_edge_style(row_index)==1? 'gray': block.border_color;

        var to_x = block.get_vertex(row_index, 0) + offsetX;
        var to_y = block.get_vertex(row_index, 1) + offsetY;
        ctx.lineTo(to_x, to_y);

        ctx.stroke();
        ctx.closePath();

        start_x = block.get_vertex(row_index, 0) + offsetX;
        start_y = block.get_vertex(row_index, 1) + offsetY;
    }
}

export const draw_shape_border = (ctx, shape, params) => {
    // Draw blocks
    for (var i = 0; i < shape.get_blocks().length; i++) {
        var block = shape.get_blocks()[i];
        draw_border(ctx, block, shape.x + params.offsetX, shape.y + params.offsetY);
    }
}

export const draw_shape = (ctx, shape, params) => {
    ctx.beginPath();
    // Draw blocks
    for (var i = 0; i < shape.get_blocks().length; i++) {
        var block = shape.get_blocks()[i];
        draw_block(ctx, block, shape.x + params.offsetX, shape.y + params.offsetY, shape.is_active(), shape.highlight);
    }
}

export const draw_line = (ctx, block, row, offsetX, offsetY) => {
    var to_x = block.get_vertex(row, 0) + offsetX;
    var to_y = block.get_vertex(row, 1) + offsetY;
    ctx.lineTo(to_x, to_y);
}

export const draw_block = (ctx, block, offsetX, offsetY, active, highlight=null) => {
    if (active) {
        ctx.shadowColor = 'grey';
        ctx.shadowBlur = 10;
    } else if (highlight) {
        ctx.shadowColor = highlight;
        ctx.shadowBlur = 10;
    } else {
        ctx.shadowBlur = 0; // set highlight invisible
    }
    ctx.fillStyle = block.color;
    ctx.strokeStyle = 'lightgray';
    ctx.lineWidth = 1;
    ctx.moveTo(block.get_vertex(0, 0) + offsetX, block.get_vertex(0, 1) + offsetY);

    for (var row = 1; row <= block.get_vertices().length; row++) {
        draw_line(ctx, block, row < block.get_vertices().length ? row : 0, offsetX, offsetY);
    }
    ctx.fill();
};