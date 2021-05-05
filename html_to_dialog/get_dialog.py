# -*- coding: utf-8 -*-

# Wencke Liermann, Lisa Plagemann, Niklas Stepczynski
# SoSe21
# Python 3.8.5
# Windows 10
"""Parse Furhat Studio website html to obtain dialog data."""

import argparse
import sys

from bs4 import BeautifulSoup


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description="Parse the website code of Furhat Studio to obtain dialog data.",
        epilog='Example: $ python get_dialog.py "Furhat Studio.html" "participant02.txt"')
    parser.add_argument('file_in',
                        metavar='SOURCE',
                        help='specify a valid path to the html file')

    parser.add_argument('file_out',
                        metavar='GOAL',
                        help='specify a filename for the extracted dialog')


    if len(sys.argv) == 1:
        parser.print_help()
    else:
        args = parser.parse_args()
        with open(args.file_in, 'r', encoding="utf-8") as html_doc:
            soup = BeautifulSoup(html_doc, 'html.parser')
        with open(args.file_out, 'w', encoding="utf-8") as file_out:
            for div in soup.find_all("div"):
                if "agentSpeech" in div.get("class", []):
                    file_out.write(f"Furhat:{div.string}\n")
                elif "userSpeech" in div.get("class", []):
                    file_out.write(f"User:{div.div.p.string}\n")
