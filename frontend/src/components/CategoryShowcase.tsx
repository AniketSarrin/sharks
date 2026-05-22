import { Music, Network, Heart } from "lucide-react";

const categoryItems = [
  { name: "Music", icon: Music },
  { name: "Networking", icon: Network },
  { name: "Dating", icon: Heart },
];

interface CategoryShowcaseProps {
  onSelect: (cat: string) => void;
}

const CategoryShowcase = ({ onSelect }: CategoryShowcaseProps) => {
  return (
    <section className="py-16">
      <div className="container mx-auto px-4">
        <div className="mb-10 text-center">
          <h2 className="text-3xl font-bold text-foreground">Browse by Category</h2>
          <p className="mt-2 text-muted-foreground">Explore events in topics you love</p>
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3 max-w-lg mx-auto w-full">
          {categoryItems.map((cat, i) => (
            <button
              key={cat.name}
              onClick={() => onSelect(cat.name)}
              className="group flex flex-col items-center gap-3 rounded-xl bg-card p-5 card-shadow transition-all hover:card-shadow-hover hover:-translate-y-1 animate-fade-in"
              style={{ animationDelay: `${i * 0.05}s` }}
            >
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-accent/10 transition-colors group-hover:bg-accent/20">
                <cat.icon className="h-6 w-6 text-accent" />
              </div>
              <span className="text-xs font-medium text-card-foreground">{cat.name}</span>
            </button>
          ))}
        </div>
      </div>
    </section>
  );
};

export default CategoryShowcase;
