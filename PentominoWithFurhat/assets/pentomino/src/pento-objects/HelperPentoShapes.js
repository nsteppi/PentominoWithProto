/**
 * Draw point (one block shape)
 */
import {Block} from "./PentoBlock";
import {Shape} from "./PentoShape";

const pento_point = (shape) => {
    var block = pento_create_block(0, 0, shape.block_size, shape.color);
    shape.add_block(block);
}

const pento_create_block = (x, y, block_size, color) => {
    return new Block(x, y, block_size, block_size, color)
};

// draw F 
const pento_F = (shape) =>  {
    // Draw blocks
    for (var y = -1; y < 2; y++) {
        shape.add_block(pento_create_block(0, + y * shape.block_size, shape.block_size, shape.color));
    }

    if (shape.is_mirrored) {
        shape.add_block(pento_create_block(shape.block_size, - shape.block_size, shape.block_size, shape.color));
        shape.add_block(pento_create_block(- shape.block_size, 0, shape.block_size, shape.color));
    } else {
        shape.add_block(pento_create_block(- shape.block_size, - shape.block_size, shape.block_size, shape.color));
        shape.add_block(pento_create_block(shape.block_size, 0, shape.block_size, shape.color));
    }
};

// Draw I
const pento_I = (shape) => {
    // Draw blocks
    for (var y = -2; y < 3; y++) {
        var block = pento_create_block(0, y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }
};

// Draw L
const pento_L = (shape) => {
    // Draw blocks
    for (var y = -2; y < 2; y++) {
        var block = pento_create_block(0, y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }

    if (shape.is_mirrored) {
        var block = pento_create_block(shape.block_size, 1 * shape.block_size, shape.block_size, shape.color);
    } else {
        var block = pento_create_block(- shape.block_size, 1 * shape.block_size, shape.block_size, shape.color);
    }
    shape.add_block(block);
};

// draw N
const pento_N = (shape) => {

    // Draw blocks
    for (var y = -1; y < 2; y++) {
        var block = pento_create_block(0, + y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }

    for (var y = -2; y < 0; y++) {
        if (shape.is_mirrored) {
            var block = pento_create_block(- shape.block_size, + y * shape.block_size, shape.block_size, shape.color);
        } else {
            var block = pento_create_block(+ shape.block_size, + y * shape.block_size, shape.block_size, shape.color);
        }
        shape.add_block(block);
    }
};

// draw P
const pento_P = (shape) => {

    // Draw blocks
    for (var y = -1; y < 2; y++) {
        var block = pento_create_block(0, y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }

    for (var y = 0; y < 2; y++) {
        if (shape.is_mirrored) {
            var block = pento_create_block(shape.block_size, + y * shape.block_size, shape.block_size, shape.color);
        } else {
            var block = pento_create_block(- shape.block_size, + y * shape.block_size, shape.block_size, shape.color);
        }
        shape.add_block(block);
    }
};

// Draw T
const pento_T = (shape) => {
    // Draw blocks (no mirrored version here)
    for (var x = -1; x < 2; x++) {
        var block = pento_create_block(+ x * shape.block_size, 0, shape.block_size, shape.color);
        shape.add_block(block);
    }

    shape.add_block(pento_create_block(shape.block_size, - shape.block_size, shape.block_size, shape.color));
    shape.add_block(pento_create_block(shape.block_size, + shape.block_size, shape.block_size, shape.color));
};

// draw U
const pento_U = (shape) => {
    // Draw blocks (no mirrored version here)
    for (var y = -1; y < 2; y++) {
        var block = pento_create_block(0, y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }

    var block = pento_create_block(shape.block_size, - shape.block_size, shape.block_size, shape.color);
    shape.add_block(block);

    var block = pento_create_block(shape.block_size, shape.block_size, shape.block_size, shape.color);
    shape.add_block(block);
};

// draw V
const pento_V = (shape) => {
    // Draw blocks (no mirrored version here)
    for (var y = -1; y < 2; y++) {
        var block = pento_create_block(- shape.block_size, y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }

    for (var x = 0; x < 2; x++) {
        var block = pento_create_block(x * shape.block_size, - shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }
};

// draw W
const pento_W = (shape) => {

    // Draw blocks (no mirrored version here)
    for (var y = -1; y < 1; y++) {
        var block = pento_create_block(- shape.block_size, y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }

    for (var y = 0; y < 2; y++) {
        var block = pento_create_block(0, y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }

    var block = pento_create_block(shape.block_size, shape.block_size, shape.block_size, shape.color);
    shape.add_block(block);
};

// Draw X
const pento_X = (shape) => {
    // Draw blocks (no mirrored version here)
    for (var y = -1; y < 2; y++) {
        var block = pento_create_block(0, y * shape.block_size, shape.block_size, shape.color);
        shape.add_block(block);
    }

    shape.add_block(pento_create_block(- shape.block_size, 0, shape.block_size, shape.color));
    shape.add_block(pento_create_block(shape.block_size, 0, shape.block_size, shape.color));
};

// Draw Y
const pento_Y = (shape) => {
    // Draw blocks
    for (var x = -2; x < 2; x++) {
        var block = pento_create_block(x * shape.block_size, 0, shape.block_size, shape.color);
        shape.add_block(block);
    }

    if (shape.is_mirrored) {
        var block = pento_create_block(0, - shape.block_size, shape.block_size, shape.color);
    } else {
        var block = pento_create_block(0, + shape.block_size, shape.block_size, shape.color);
    }
    shape.add_block(block);
};

// draw Z
const pento_Z = (shape) => {
    // Draw blocks
    for (var x = -1; x < 2; x++) {
        var block = pento_create_block(x * shape.block_size, 0, shape.block_size, shape.color);
        shape.add_block(block);
    }

    if (shape.is_mirrored) {
        shape.add_block(pento_create_block(- shape.block_size, - shape.block_size, shape.block_size, shape.color));
        shape.add_block(pento_create_block(shape.block_size, shape.block_size, shape.block_size, shape.color));
    } else {
        shape.add_block(pento_create_block(- shape.block_size, shape.block_size, shape.block_size, shape.color));
        shape.add_block(pento_create_block(shape.block_size, -  shape.block_size, shape.block_size, shape.color));
    }

};


const _new_pento_shape = (id, type, color, is_mirrored, rotation, block_size) => {
    return new Shape(id, type, color, is_mirrored, rotation == null ? 0 : rotation, block_size)
};

export const pento_create_shape = (id, x, y, type, color, is_mirrored, rotation, block_size) => {
    //create empty shape
    var new_shape = _new_pento_shape(id, type, color, is_mirrored, 0, block_size);

    switch (type) {
        case 'point':
            pento_point(new_shape);
            break;
        case 'F':
            pento_F(new_shape);
            break;
        case 'I':
            pento_I(new_shape);
            break;
        case 'L':
            pento_L(new_shape);
            break;
        case 'N':
            pento_N(new_shape);
            break;
        case 'P':
            pento_P(new_shape);
            break;
        case 'T':
            pento_T(new_shape);
            break;
        case 'U':
            pento_U(new_shape);
            break;
        case 'V':
            pento_V(new_shape);
            break;
        case 'W':
            pento_W(new_shape);
            break;
        case 'X':
            pento_X(new_shape);
            break;
        case 'Y':
            pento_Y(new_shape);
            break;
        case 'Z':
            pento_Z(new_shape);
            break;
        default:
            console.log('Unsupported shape type: ' + type);
            return;
    }

    // Important: Closing the shapes disabled editing and
    // calculates center point for rotations
    new_shape.close();

    // move and rotate
    new_shape.moveTo(x, y);
    new_shape.rotate(rotation);

    return new_shape
};