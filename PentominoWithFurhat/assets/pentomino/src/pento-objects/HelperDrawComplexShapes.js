import {grid_cell_to_coordinates} from "./HelperDrawingBoard";
import {pento_create_shape} from "./HelperPentoShapes";
import {PentoConfig} from "../config";

export const configPerShape = (shape, n_blocks) => {

    let shape_config = {"x": -1, "y": -1, "coords": {}}

    switch (shape) {

        case "elephant":
            shape_config["x"] = Math.max(0, Math.floor((n_blocks-11) / 2));
            shape_config["y"] = Math.max(0, Math.floor((n_blocks-9) / 2));
            shape_config["coords"] = {
                'F': {'x':4 , 'y':4},
                'I': {'x':3 , 'y':6},
                'L': {'x':10 , 'y':7},
                'N': {'x':4 , 'y':7},
                'P': {'x':7 , 'y':3},
                'T': {'x':8 , 'y':6},
                'U': {'x':0 , 'y':1},
                'V': {'x':1 , 'y':4},
                'W': {'x':9 , 'y':3},
                'X': {'x':2 , 'y':1},
                'Y': {'x':5 , 'y':2},
                'Z': {'x':7 , 'y':5}
            };
            return shape_config;

        case "upper_left_corner":
            shape_config["x"] = 0;
            shape_config["y"] = 0;
            for (let type of ['F', 'I', 'L', 'N', 'P', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']) {
                shape_config["coords"][type] = {'x':3, 'y': 3};
            }
            return shape_config;
        default:
            return shape_config
    }
};


/**
 * This generates all shapes that are required to fill the elephant
 */
export const generateElephantShape = (shape, pento_config, grid_config) => {
    // set value ranges for random selection
    const columns =			[...Array(grid_config.n_blocks).keys()];
    const rows =				[...Array(grid_config.n_blocks).keys()];

    // generation parameters
    const colors =			pento_config.get_pento_colors();
    const pento_types =		pento_config.get_pento_types();

    let generated_shapes = 	[];
    let template_shapes = [];

    for (let id=0; id<pento_types.length; id++) {
        let rand_color = colors[Math.floor(Math.random() * colors.length)];
        // while rand_color grey choose new color
        while (pento_config.get_color_name(rand_color) == pento_config.templ_col) {
            rand_color = colors[Math.floor(Math.random() * colors.length)];
        }

        const pento_types =		pento_config.get_pento_types();
        let pento_piece = pento_types[id]

        let new_shape = createNewPentoPieceInShape(shape, grid_config, pento_piece, rand_color, id);
        let new_template = createNewPentoPieceInShape(shape, grid_config, pento_piece, pento_config.get_hex_code(pento_config.templ_col), id);
        generated_shapes.push(new_shape.copy(id));
        template_shapes.push(new_template.copy(pento_types.length + id));
    }


    // now move, rotate, flip shape randomly to create initial board
    create_initial_state(generated_shapes, ['rotate', 'move', 'flip'], grid_config);

    return [generated_shapes, template_shapes];
};

export const createNewPentoPieceInShape = (shape, grid_config, pento_piece, color, id) => {
    const shape_config = configPerShape(shape, grid_config.n_blocks)

    // place on elephant board (predefined position)
    let eleX = shape_config["x"] + shape_config["coords"][pento_piece]['x'];
    let eleY = shape_config["y"] + shape_config["coords"][pento_piece]['y'];
    let coords = grid_cell_to_coordinates(eleX, eleY, grid_config.block_size);

    //console.log({"id":id, "x": coords[0], "y": coords[1], "type": pento_types[id], "color": rand_color})
    // create shape for the elephant board: without flip or rotation
    let new_shape = pento_create_shape(id, coords[0], coords[1], pento_piece, color, false, 0, grid_config.block_size);

    return new_shape.copy(id)
}

/**
 * Create initial state by manipulating the target state by n actions
 * @param {copy of target shapes} shapes
 * @param {actions} nactions
 */
const create_initial_state = (shapes, actions, grid_config) => {
    // likelihood of an action (rotate, flip) being performed
    let action_likelihood = 0.5;
    let action_counter = 0;

    // remove action 'connect' if present
    if (actions.includes('connect')) {
        actions.splice(actions.indexOf('connect'), 1);
    }
    for (let shape of shapes) {
        //this.pento_board_initial.place_shape(shape);

        // MOVE, ROTATE, FLIP
        for (let action of ['move', 'rotate', 'flip']) {
            if (!actions.includes(action)) {
                console.log('PentoBoard does not support action ' + action);
            } else {
                // Always perform 'move'. Decide randomly whether to perform 'rotate' and 'flip'
                if (action == 'move' || Math.random() < action_likelihood) {
                    // generate a valid set of parameters
                    let params = generate_params(shape, action, grid_config);
                    execute_action(action, shape, params);
                    let attempts = 0; // assure loop termination if action is impossible due to board size
                    while (!isValidAction(action, shape, shapes, grid_config) && attempts < 40) {
                        shape.rollback(1);
                        params = generate_params(shape, action, grid_config);
                        execute_action(action, shape, params);
                        ++attempts;
                    }
                    // emit warning if invalid parameters where used
                    if (attempts >= 40) {
                        console.log(`No valid parameters were found for shape ${shape.name} and action ${action} during ${attempts} iterations. Result may contain overlaps.`);
                        continue;
                    }

                    ++action_counter;
                }
            }
        }
    }
};

const execute_action = (action_name, shape, params) => {
    switch (action_name) {
        case 'move':
            shape.moveTo(params['x'], params['y']);
            break;
        case 'rotate':
            shape.rotate(params['rotation']);
            break;
        case 'flip':
            shape.flip(params['axis']);
            break;
        default:
            console.log('Unknown action: ' + action_name);
    }
};

/**
 *
 * @param {randomly selected shape} rand_shape
 * @param {one of ['move', 'rotate']} action_type
 * @param {copy of target shapes} shapes
 */
const generate_params = (rand_shape, action_type, grid_config) => {
    var max = grid_config.board_size;
    var min = 0;
    var rotations = [90, 180, 270];
    var axis = ['vertical']; // Only use one type of mirroring during generation / else ['horizontal', 'vertical'];

    switch (action_type) {
        case 'move':
            let rand_x = random_in_range(min, max, grid_config.block_size);
            let rand_y = random_in_range(min, max, grid_config.block_size);
            return { 'x': rand_x, 'y': rand_y };
        case 'rotate':
            let rand_angle = rotations[Math.floor(Math.random() * rotations.length)];
            return { 'rotation': rand_angle };
        case 'flip':
            return { 'axis': 'vertical' } // always flip vertically during generation for now
        //let rand_axis = axis[Math.floor(Math.random() * axis.length)];
        //return { 'axis': rand_axis };
        default:
            console.log('Not implemented: ' + action_type);
            return;
    }
};

/**
 * Retrieve random number rn with rn >= min and rn <= max
 *
 * @param {int} min
 * @param {int} max
 * @param {int} step
 */
const random_in_range = (min, max, step=1) => {
    return (Math.floor(Math.random() * ((max - min)/step)) + min) * step
};

/**
 * Checks if action and shape are valid considering the current board state
 * @param {*} action_name
 * @param {*} shape
 * @param {*} params
 */
const isValidAction = (action_name, shape, pento_shapes, grid_config) => {
    // make extra check for place as this is a one time action
    if (shape.is_inside(grid_config.x, grid_config.y, grid_config.x+grid_config.board_size, grid_config.y+grid_config.board_size)) {
        switch (action_name) {
            case 'place':
                if (!has_collisions(shape, pento_shapes)) {
                    return true;
                }
                break;
            case 'move':
                if (!has_collisions(shape, pento_shapes) && !shape.has_connections()) {
                    return true;
                }
                break;
            case 'rotate':
                if (!has_collisions(shape, pento_shapes) && !shape.has_connections()) {
                    return true;
                }
                break;
            case 'flip':
                if (!has_collisions(shape, pento_shapes) && !shape.has_connections()) {
                    return true;
                }
                break;
        }
    }
    return false;
};

/**
 * Is true when at least one shape collides with this shape
 * @param {shape to check for} shape
 */
const has_collisions = (shape, pento_shapes) => {
    return get_collisions(shape, pento_shapes).length > 0
}

/**
 * Returns a list of shapes colliding with shape
 * @param {shape to check for} shape
 */
const get_collisions = (shape, pento_shapes) => {
    let hits = [];
    for (let key in pento_shapes) {
        let other_shape = pento_shapes[key];
        if (other_shape.name != shape.name) {
            if (shape.hits(other_shape)) {
                hits.push(other_shape);
            }
        }
    }
    return hits;
}