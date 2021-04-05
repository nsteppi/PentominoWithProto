import {pento_create_shape} from "./HelperPentoShapes";

export class Shape {
    constructor(id, type, color, is_mirrored, rotation, block_size) {
        this.id = id;
        this.x = 0;
        this.y = 0;
        this.type = type;
        this.color = color;
        this.rotation = rotation;
        this.is_mirrored = is_mirrored || false;
        this.writable = true;
        this.active = false;
        this.highlight = false;

        // shape internal grid and bounding box
        this._internal_grid_size = [5, 5];
        this._internal_grid_shifts = [2, 2];
        this._internal_grid = [];
        this._init_grid();

        // log changes for rollback
        this.changes = [];

        // generate name
        this.name = this.type + this.id + this.color;
        this.blocks = [];
        this.block_size = block_size;

        // conntected shapes
        this.connected = [];
    }

    /**
     * Returns true if the shape is currently
     * active and can be modified
     */
    is_active() {
        return this.active
    }

    set_active() {
        this.active = true;
    }

    set_deactive() {
        this.active = false;
    }

    /**
     * Toggle colored hightlighting of shape on and off
     * @param {highlight color} color
     */
    set_highlight(color) {
        this.highlight = color;
    }

    remove_highlight() {
        this.highlight = null;
    }

    /**
     * Checks whether the shape is inside the bounding box or has overlaps (returns false if any part of the shape is
     * outside the bounding box)
     * @param {Bound Box x} bb_x
     * @param {Bounding Box y} bb_y
     * @param {Bounding Box Width} bb_width
     * @param {Bounding Box Height} bb_height
     */
    is_inside(bb_x, bb_y, bb_width, bb_height) {
        var bounding_box = [bb_x, bb_y, bb_width, bb_height];
        for (var block_index in this.blocks) {
            var block = this.blocks[block_index];
            if (!block.is_inside(bounding_box, this.x, this.y)) {
                return false
            }
        }
        return true
    }

    /**
     * Returns (x,y) position
     */
    get_coords() {
        return [this.x, this.y]
    }

    /**
     * Checks whether these shapes are connected
     * @param {shape} other_shape
     */
    is_connected(other_shape) {
        return this.connected.indexOf(other_shape.name) != -1
    }

    /**
     * Checks whether this shape is connected to any other
     * shape
     */
    has_connections() {
        return this.connected.length > 0
    }

    /**
     * Return the internal grid matrix
     */
    get_internal_grid() {
        return this._internal_grid
    }

    /**
     * Return width of shape (number of horizontal blocks)
     */
    get_grid_width() {
        return this._internal_grid_size[0]
    }

    /**
     * Return height of shape (number of vertical blocks)
     */
    get_grid_height() {
        return this._internal_grid_size[1]
    }

    /**
     * Estimates the relative position of the
     * other shape compared to this shape and
     * returns a direction of connection
     * @param {shape to connect to} other_shape
     */
    get_direction(other_shape) {
        var delta_x = other_shape.x - this.x;
        var delta_y = other_shape.y - this.y;
        var directions = ['top', 'left', 'bottom', 'right'];

        // select direction based on relative position difference
        if (delta_x >= 0 && delta_y <= 0) {
            return directions[0]
        } else if (delta_x <= 0 && delta_y >= 0) {
            return directions[1]
        } else if (delta_x >= 0 && delta_y >= 0) {
            return directions[2]
        } else {
            return directions[3]
        }
    }

    /**
     * Copy matrix
     * @param {Creates a deepcopy of a matrix} matrix
     */
    copy_matrix(matrix) {
        var new_matrix = [];
        for (var i = 0; i < matrix.length; i++) {
            var row = [];
            for (var e = 0; e < matrix[i].length; e++) {
                row.push(matrix[i][e]);
            }
            new_matrix.push(row);
        }
        return new_matrix
    }


    /**
     * Copy and rotate matrix 90 degrees clockwise
     * @param {*} matrix
     */
    copy_and_rotate(matrix) {
        // copy
        var a = this.copy_matrix(matrix);

        // rotate
        var N = a.length;
        for (var i = 0; i < (N / 2 | 0); i++) {
            for (var j = i; j < N - i - 1; j++) {
                var temp = a[i][j];
                a[i][j] = a[N - 1 - j][i];
                a[N - 1 - j][i] = a[N - 1 - i][N - 1 - j];
                a[N - 1 - i][N - 1 - j] = a[j][N - 1 - i];
                a[j][N - 1 - i] = temp;
            }
        }
        return a
    }

    /**
     * Returns the index of the first blocking column
     * from the right
     * @param {internal matrix} matrix
     */
    get_right_fbc(matrix) {
        for (var i = matrix.length - 1; i >= 0; i--) {
            if (matrix[i].indexOf(1) != -1)
                break;
        }
        return i
    }

    /**
     * Retrieves the index of the first blocking column
     * from the left
     * @param {internal matrix} matrix
     */
    get_left_fbc(matrix) {
        for (var i = 0; i < matrix.length; i++) {
            if (matrix[i].indexOf(1) != -1)
                break;
        }
        return i
    }

    get_movement(look_left, fbc, fbc2, matrix) {
        return [0, 0]
    }

    /**
     * Connects to shapes
     * @param {shape to connect to} other_shape
     * @param {direction of connection} direction
     */
    align_and_connect(other_shape, direction) {
        // get copy of matrix for inplace operations and rotate if necessary
        if (direction == 'top' || direction == 'bottom') {
            var matrix = this.copy_and_rotate(other_shape.get_internal_grid());
        } else {
            var matrix = this.copy_matrix(other_shape.get_internal_grid());
        }

        // index of left or right first blocking column respectively
        var look_left = (direction == 'top' || direction == 'right');
        var fbc = look_left ? this.get_right_fbc(matrix) : this.get_left_fbc(matrix);
        var fbc2 = look_left ? this.get_left_fbc(matrix) : this.get_right_fbc(matrix);

        // move other shape to new position
        var new_positions = this.get_movement(look_left, fbc, fbc2, matrix);
        other_shape.moveTo(new_positions[0], new_positions[1]);
    }

    connect_to(other_shape) {
        // align internal grids
        other_shape.rotate(this.rotation - other_shape.rotation);

        // connect grids so that the resulting matrix doesnt contain a two (after adding both together)
        // move shapes close together
        //var direction = this.get_direction(other_shape)
        //this.align_and_connect(other_shape, direction)

        // register connection
        //this.connected.push(other_shape.name)
        //other_shape.connected.push(this.name)

        return 'group' + this.id + other_shape.id
    }

    /**
     * Creates the datastructure for the internal grid
     */
    _init_grid() {
        for (var i = 0; i < this._internal_grid_size[0]; i++) {
            this._internal_grid.push([]);
            for (var e = 0; e < this._internal_grid_size[1]; e++) {
                this._internal_grid[i].push(0);
            }
        }
    }

    /**
     * Sets the value at (row, col)
     * @param {row} row
     * @param {col} col
     * @param {value for cell} value
     */
    _set_grid_value(row, col, value) {
        //console.log(this._internal_grid)
        //console.log(row)
        this._internal_grid[row][col] = value;
    }

    /**
     * Marks the position of a block on the internal grid with 1
     * @param {x} block_x
     * @param {y} block_y
     */
    _update_grid(block_x, block_y) {
        var row = (block_y / this.block_size) + this._internal_grid_shifts[1];
        var col = (block_x / this.block_size) + this._internal_grid_shifts[0];
        this._set_grid_value(row, col, 1);
    }

    /**
     * Rolls back N steps of modifications done to the shape (except initial placement)
     * @param {int} steps
     */
    rollback(steps) {
        if (this.changes.length > 0) {
            for (var i = (this.changes.length - 1); i >= Math.max(0, this.changes.length - steps); i--) {
                this.undo_action(this.changes[i]);
            }
            this.changes = this.changes.slice(0, Math.max(0, this.changes.length - steps));
        }
    }

    /**
     * Restores the state of the shape before the modification
     * @param {action object} action
     */
    undo_action(action) {
        switch (action['name']) {
            case 'move':
                this.moveTo(action['x'], action['y'], false);
                break;
            case 'rotate':
                this.rotate(360 - action['angle'], false);
                break;
        }
    }

    /**
     * Returns the true angle for rotation
     * @param {degree} angle
     */
    _get_true_angle(angle) {
        var true_angle = (this.rotation + angle) % 360;
        return true_angle
    }

    /**
     * Helper that updates the rotation of internal block model
     */
    _rotate_blocks(delta_angle) {
        for (var i = 0; i < this.get_blocks().length; i++) {
            var block = this.get_blocks()[i];
            block.rotate(delta_angle, this.rotation);
        }
    }

    /**
     * Rotates the shape by angle
     * @param {difference to current angle in degrees} angle
     */
    rotate(angle) {
        if (angle == 0) { return; }

        this.rotation = this._get_true_angle(angle);
        // use rearrange method if possible
        if (angle % 90 == 0) {
            this.rotateByRearrange(angle);
        } else {
            this._rotate_blocks(angle);
        }
    }

    /**
     * Updates the block edge styles.
     * To be used after rearranging blocks so that the black outline still
     * matches the shape formation.
     */
    _update_outline() {
        for (var i = 0; i < this.get_blocks().length; i++) {
            var block = this.get_blocks()[i];
            var adjacent_blocks = this.get_adjacent_blocks(block.get_x(), block.get_y());
            for (var a = 0; a < adjacent_blocks.length; a++) {
                if (adjacent_blocks[a] == 0) {
                    block.set_edge_style(a, 2);
                } else {
                    block.set_edge_style(a, 0.5);
                }
            }
        }
    }

    /**
     * Rotates the shape by angle. This implementation rearranges the blocks on the
     * internal matrix. Only angles 90, -90, 180 and -180 are possible
     * @param {turning angle, one of [90,-90,180,-180]} angle
     * @param {true to log the action to the changes array} track
     */
    rotateByRearrange(angle, track) {
        if (track != false) {
            this.changes.push({ 'name': 'rotate', 'angle': angle });
        }
        this.rotation = this._get_true_angle(angle);
        // empty the grid (since the blocks will be rearranged)
        this._empty_grid();
        for (var i = 0; i < this.get_blocks().length; i++) {
            var block = this.get_blocks()[i];
            block.rotateByRearrange(angle, this.rotation);
            // update the internal grid
            this._update_grid(block.get_x(), block.get_y());
        }
        // redraw the outline
        this._update_outline();
    }

    /**
     * Flips the shape by rearranging the blocks
     * @param {one of ['horizontal', 'vertical']} axis
     * @param {true to log the action to the changes array} track
     */
    flip(axis, track) {
        if (track != false) {
            this.changes.push({ 'name': 'flip', 'axis': axis});
        }
        // empty grid (since blocks will be rearranged)
        this._empty_grid()
        for (var i = 0; i < this.get_blocks().length; i++) {
            var block = this.get_blocks()[i];
            block.flip(axis);
            this._update_grid(block.get_x(), block.get_y());
        }
        // redraw the black outline after block rearrangement
        this._update_outline();
        this.is_mirrored = !this.is_mirrored;
    }

    /**
     * Moves shape to a fixed position
     * @param {x} x
     * @param {y} y
     * @param {if this action should be tracked (can't be undone otherwise)} track
     */
    moveTo(x, y, track) {
        if (track != false) {
            this.changes.push({ 'name': 'move', 'x': this.x, 'y': this.y});
        }
        this.x = x;
        this.y = y;
    }

    /**
     * Open for changes
     */
    open() {
        this.writable = true;
    }

    /**
     * Finalizes the shape
     * No further modifications are allowed after
     * this function is called
     */
    close() {
        this.writable = false;

        var x_sum = 0;
        var y_sum = 0;

//			for (var block_index in this.blocks) {
//				var block_center = this.blocks[block_index].get_center();
//				x_sum += block_center[0];
//				y_sum += block_center[1];
//			}
//			// this leads to nice turning, but doesn't align shape with grid
//			var center_x = x_sum / this.blocks.length;
//			var center_y = y_sum / this.blocks.length;
        var center_x = 20;
        var center_y = 20;

        // update blocks
        for (var block_index in this.blocks) {
            var block = this.blocks[block_index];
            block.set_shape_center(center_x, center_y);

            // update block styles
            var adjacent_blocks = this.get_adjacent_blocks(block.get_x(), block.get_y());
            for (var i = 0; i < adjacent_blocks.length; i++) {
                if (adjacent_blocks[i] === 0) {
                    block.set_edge_style(i, 2);
                }
            }
        }
    }

    /**
     * Retrieve a matrix (top, right, bottom, left) which represents
     * adjacent blocks with 1 (0 if no block is adjacent on a side)
     * @param {block x} x
     * @param {block y} y
     */
    get_adjacent_blocks(x, y) {
        var row = Math.round(y / this.block_size) + this._internal_grid_shifts[1];
        var col = Math.round(x / this.block_size) + this._internal_grid_shifts[0];
        var adjacent_matrix = [0,0,0,0];
        // top
        if (row - 1 >= 0) {
            adjacent_matrix[1] = this._internal_grid[row - 1][col];
        }

        // right
        if (col + 1 < this._internal_grid[row].length) {
            adjacent_matrix[2] = this._internal_grid[row][col + 1];
        }

        // bottom
        if (row + 1 < this._internal_grid.length) {
            adjacent_matrix[3] = this._internal_grid[row + 1][col];
        }

        // left
        if (col - 1 >= 0) {
            adjacent_matrix[0] = this._internal_grid[row][col - 1];
        }

        return adjacent_matrix
    }

    /**
     * Adds a block to the shape if it's writable
     * @param {block object} block
     */
    add_block(block) {
        if (this.writable) {
            this.blocks.push(block);
            this._update_grid(block.x, block.y);
        }
    }

    /**
     * Returns all blocks of the shape
     */
    get_blocks() {
        return this.blocks
    }

    /**
     * Checks whether the shapes hit each other
     * @param {shape to compare} other_shape
     */
    hits(other_shape) {
        // calculate delta between shapes
        var dx = this.x - other_shape.x;
        var dy = this.y - other_shape.y;

        for (var index in this.blocks) {
            var current_block = this.blocks[index];
            var other_blocks = other_shape.get_blocks();

            for (var o_index in other_blocks) {
                var other_block = other_blocks[o_index];
                if (current_block.hits(other_block, dx, dy)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Returns a deep copy of the shape with the new id assigned
     * @param {id} new_id
     */
    copy(new_id) {
        var shape_copy = pento_create_shape(new_id, this.x, this.y, this.type, this.color,
            this.is_mirrored, this.rotation, this.block_size);
        shape_copy.width = this.width;
        shape_copy.height = this.height;
        return shape_copy
    }

    /**
     * Adapts the shape's coordinates to a differently sized board.
     * @param {new block size} block_size
     * @param {new board size / old board size} pos_factor
     */
    scale(block_size, pos_factor) {
        // change block sizes
        this.block_size = block_size;
        // scale coordinates
        this.x *= pos_factor;
        this.y *= pos_factor;
        // scale each block
        var blocks = [];
        for (var b of this.get_blocks()) {
            blocks.push(new document.Block(	b.x * pos_factor,
                b.y * pos_factor,
                block_size,
                block_size,
                b.color));
        }
        this.blocks = blocks;
    }

    /**
     * Fills the internal grid with zeros, i.e. deletes all block markers
     */
    _empty_grid() {
        for (var x = 0; x < this._internal_grid_size[0]; x++) {
            for (var y = 0; y < this._internal_grid_size[1]; y++) {
                this._set_grid_value(x, y, 0);
            }
        }
    }



    toString() {
        return this.name
    }
};
