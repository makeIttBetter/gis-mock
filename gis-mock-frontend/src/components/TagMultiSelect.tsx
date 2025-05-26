// file: src/components/TagMultiSelect.tsx
"use client";
import React, {useEffect, useRef, useState} from "react";

/**
 * Props for a tag-based multi-select component (like LinkedIn Skills picker).
 */
interface TagMultiSelectProps {
    /** All possible options that can be selected. */
    availableOptions: string[];

    /** The currently chosen tags (user's selections). */
    selectedValues: string[];

    /** Called whenever the list of selected values changes (adds/removes). */
    onChange: (newValues: string[]) => void;

    /** Placeholder text inside the text input. */
    placeholder?: string;
}

/**
 * A multi-select input that allows the user to pick multiple items as "tags."
 * The user can type to filter availableOptions, then click on an option to add it.
 */
const TagMultiSelect: React.FC<TagMultiSelectProps> = ({
                                                           availableOptions,
                                                           selectedValues,
                                                           onChange,
                                                           placeholder = "Select..."
                                                       }) => {
    const [inputValue, setInputValue] = useState("");
    const [showDropdown, setShowDropdown] = useState(false);
    const containerRef = useRef<HTMLDivElement | null>(null);

    // Filter out any already-selected items + match the typed text
    const filteredOptions = availableOptions
        .filter((opt) => opt.toLowerCase().includes(inputValue.toLowerCase().trim()))
        .filter((opt) => !selectedValues.includes(opt));

    // Close dropdown if user clicks outside
    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setShowDropdown(false);
            }
        }

        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // When the user types, open the dropdown and update inputValue
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setInputValue(e.target.value);
        setShowDropdown(true);
    };

    // Add a tag to the selected values
    const handleAddTag = (tag: string) => {
        const newSelected = [...selectedValues, tag];
        onChange(newSelected);
        setInputValue("");
        setShowDropdown(false);
    };

    // Remove a tag from the selection
    const handleRemoveTag = (tag: string) => {
        const newSelected = selectedValues.filter((item) => item !== tag);
        onChange(newSelected);
    };

    return (
        <div ref={containerRef} className="relative">
            {/* Display the "chips" (selectedValues) */}
            <div className="flex flex-wrap gap-2 mb-2">
                {selectedValues.map((tag) => (
                    <div
                        key={tag}
                        className="flex items-center space-x-1 bg-blue-100 text-blue-800 px-2 py-1 rounded"
                    >
                        <span>{tag}</span>
                        <button
                            type="button"
                            className="hover:text-red-600 font-bold cursor-pointer"
                            onClick={() => handleRemoveTag(tag)}
                        >
                            x
                        </button>
                    </div>
                ))}
            </div>

            {/* Text input where user types to find an option */}
            <input
                type="text"
                className="w-full border rounded p-2"
                value={inputValue}
                onChange={handleInputChange}
                onFocus={() => setShowDropdown(true)}
                placeholder={placeholder}
            />

            {/* Dropdown with filtered suggestions */}
            {showDropdown && filteredOptions.length > 0 && (
                <ul
                    className="
            absolute
            z-10
            bg-white
            border
            shadow
            rounded
            w-full
            mt-1
            max-h-48
            overflow-auto
            min-w-[200px]
          "
                >
                    {filteredOptions.map((option) => (
                        <li
                            key={option}
                            className="px-2 py-1 hover:bg-gray-100 cursor-pointer"
                            onClick={() => handleAddTag(option)}
                        >
                            {option}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default TagMultiSelect;
