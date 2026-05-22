import { categories } from "@/lib/mock-data";

interface CategoryFilterProps {
  selected: string;
  onSelect: (cat: string) => void;
}

const CategoryFilter = ({ selected, onSelect }: CategoryFilterProps) => {
  return (
    <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
      <button
        onClick={() => onSelect("")}
        className={`shrink-0 rounded-full px-4 py-2 text-sm font-medium transition-colors ${
          selected === ""
            ? "bg-primary text-primary-foreground"
            : "bg-secondary text-secondary-foreground hover:bg-secondary/80"
        }`}
      >
        All Events
      </button>
      {categories.map((cat) => (
        <button
          key={cat}
          onClick={() => onSelect(cat)}
          className={`shrink-0 rounded-full px-4 py-2 text-sm font-medium transition-colors ${
            selected === cat
              ? "bg-primary text-primary-foreground"
              : "bg-secondary text-secondary-foreground hover:bg-secondary/80"
          }`}
        >
          {cat}
        </button>
      ))}
    </div>
  );
};

export default CategoryFilter;
