export class PentoConfig {

    /**
     * Constructor
     * Board size and number of blocks per row/column determines block size
     *
     * @param {width/height of square board in pixels, default: 400} board_size
     * @param {number of blocks in a row or column, default: 20} n_blocks
     */
    constructor(board_size = 400, n_blocks = 20) {
        this.color_map = {
            '#EEAAAA': 'light red',
            '#DDBB99': 'beige',
            '#FFFF80': 'yellow',
            '#BFFF80': 'light green',
            '#408000': 'dark green',
            '#DD99BB': 'pink',
            '#CC88CC': 'purple',
            '#99BBDD': 'light blue',
            '#336699': 'dark blue',
            '#5CD6D6': 'turquoise',
            '#FFB366': 'orange',
            '#e8e8e8': 'gray'
        };

        this.templ_col = 'gray'
        this.provide_template = true
        this.board_size = board_size;
        this.n_blocks = n_blocks;
        this.block_size = board_size / n_blocks;
        this.rotation_step = 45;
    }

    /**
     * @return array of actions that can be performed on a PentoShape
     */
    get_pento_shape_actions(){
        return ['move', 'rotate']
    }

    /**
     * @return array of possible PentoShape colors
     */
    get_pento_colors() {
        // get keys of color map
        let colors = [];
        for (let c in this.color_map) {
            colors.push(c);
        }
        return colors
    }

    get_color_name(color_code) {
        return this.color_map[color_code]
    }

    get_hex_code(color_name) {
        for (let c in this.color_map) {
            if (this.color_map[c] == color_name) {
                return c
            }
        }
        return "" //TODO: throw error
    }

    /**
     * @return array of PentoShape types
     */
    get_pento_types() {
        return ['F', 'I', 'L', 'N', 'P', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']
    }


}