import React, {Component, useEffect, useRef} from "react";
import {draw_shape, draw_shape_border} from "./HelperDrawingBlocks";
import {useInterval} from "../helper/useInterval";

export const PentoBoard = ({text, shapes, config, activeShape}) => {

    const canvasRef = useRef(null)

    const title         = text || "test";
    const pento_config  = config;
    const pento_shapes  = shapes;
    const active_shape  = activeShape

    // board size and grid parameters
    const provide_template  = config.provide_template
    const pento_grid_cols	= config.n_blocks;
    const pento_grid_rows	= config.n_blocks;
    const width				= config.board_size;
    const height			= config.board_size;
    const pento_block_size	= config.block_size;
    const pento_grid_color  = 'gray';
    const pento_grid_x      = config.x;
    const pento_grid_y      = config.y;

    // pento game parameters
    const show_grid = config.show_gridlines;

    /**
     * This re-draws the elements on the canvas every 200 ms
     */
    useInterval(() => draw_all(), 200);


    const draw_all = () => {
        const canvas = canvasRef.current
        const context = canvas.getContext('2d')

        init_board(canvas, context)
        draw(context)
    };

    /**
     * Add the board to the empty canvas
     */
    const init_board = (canvas, ctx) => {
        // set board measurements
        canvas.height = height;
        canvas.width = width;

        // draw the outer borders
        draw_line(ctx, pento_grid_x, pento_grid_y, pento_grid_x+width, pento_grid_y, 'black')
        draw_line(ctx, pento_grid_x+width, pento_grid_y, pento_grid_x+width, pento_grid_y+height, 'black')
        draw_line(ctx, pento_grid_x, pento_grid_y+height, pento_grid_x+width, pento_grid_y+height, 'black')
        draw_line(ctx, pento_grid_x, pento_grid_y, pento_grid_x, pento_grid_y+height, 'black')

        // draw gridlines
        if (show_grid) {
            for (var i = 0; i <= pento_grid_rows; i++) {
                draw_line(ctx, pento_grid_x, pento_grid_y + i * pento_block_size,
                    pento_grid_x + width, pento_grid_y + i * pento_block_size, pento_grid_color);
            }
            for (var i = 0; i <= pento_grid_cols; i++) {
                draw_line(ctx,pento_grid_x + i * pento_block_size, pento_grid_y + 0,
                    pento_grid_x + i * pento_block_size, pento_grid_y + height, pento_grid_color);
            }
        }
    };

    /**
     * Add the PentoShapes to the board
     */
    const draw = ctx => {
        pento_shapes.forEach((s) => {
            if (active_shape == null || s.name != active_shape.name) {
                s.remove_highlight()
                // don't render the template if the user does not wish to see it
                if (s.color !== pento_config.get_hex_code(pento_config.templ_col) || provide_template) {
                    draw_shape(ctx,s,{offsetX: 0, offsetY: 0})
                    draw_shape_border(ctx,s,{offsetX: 0, offsetY: 0})
                }
            }
        })
        // make sure to draw active shape last
        if (active_shape != null) {
            active_shape.set_highlight("red");
            draw_shape(ctx, active_shape, {offsetX: 0, offsetY: 0});
            draw_shape_border(ctx, active_shape, {offsetX: 0, offsetY: 0});
        }
    };


    const draw_line = (ctx, x, y, x2, y2, color) => {
        ctx.beginPath();
        ctx.moveTo(x, y);
        ctx.lineTo(x2, y2);
        ctx.strokeWidth = 1;
        ctx.strokeStyle = color;
        ctx.stroke();
    };


    return <canvas ref={canvasRef} />
}
