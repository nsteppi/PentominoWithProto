## What to do
+ (recommended) Install Miniconda.
+ Navigate into the directory of this ReadMe.
+ Create a virtual environment with all dependencies:
   ```sh
   $ conda env create -f environment.yml
   ```
+ Activate the environment:
   ```sh
   $ conda activate furhat
   ```
+ Download the Furhat Studio website:
    + Right-click -> Seite speichern unter ...
+ Extract the dialog:
    + e.g.
    ```sh
        python get_dialog.py "Furhat Studio.html" "participant02.txt"
    ```