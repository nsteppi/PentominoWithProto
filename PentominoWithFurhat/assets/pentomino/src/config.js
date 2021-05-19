export class PentoConfig {
    /**
     * Constructor
     * Board size and number of blocks per row/column determines block size
     *
     * @param board_size width/height of square board in pixels, default: 400
     * @param n_blocks number of blocks in a row or column, default: 20
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
            '#e8e8e8': 'grey'
        };
        // board properties
        this.board_size = board_size;
        this.n_blocks = n_blocks;
        this.block_size = board_size / n_blocks;
        this.x = 0
        this.y = 0
        this.show_gridlines = true

        // game mode
        this.templ_col = 'grey'
        this.provide_template = true
        this.hide_buttons = true
        this.game_time = 600
        this.demo_time = 300
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

    /**
     * @param color_code hexacode of a color
     * @return a string specifier assigned to this hexacode
     */
    get_color_name(color_code) {
        return this.color_map[color_code]
    }

    /**
     * @param color_name string specifier of a color
     * @return hexacode assigned to this color
     */
    get_hex_code(color_name) {
        for (let c in this.color_map) {
            if (this.color_map[c] === color_name) {
                return c
            }
        }
        return null
    }

    /**
     * @return Array of PentoShape types
     */
    get_pento_types() {
        return ['F', 'I', 'L', 'N', 'P', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']
    }
}
