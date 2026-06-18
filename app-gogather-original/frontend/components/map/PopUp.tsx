import { X } from 'lucide-react';

interface PopupRoleProps {
  name: string;
  category: string;
  onClose?: () => void;
}

const PopupRole = ({ name, category, onClose }: PopupRoleProps) => {
  return (
    <div className="relative w-56 rounded-xl bg-white p-4 shadow-lg">
      <button
        onClick={onClose}
        className="absolute right-2 top-2 flex h-8 w-8 items-center justify-center rounded-full text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-700 active:scale-95"
        aria-label="Fechar"
      >
        <X className="h-4 w-4 stroke-[2.5]" />
      </button>

      <div className="pr-6">
        <h3 className="text-lg font-bold text-gg-cyan" title={name}>
          {name}
        </h3>
        <p className="text-sm text-gray-500 capitalize">{category}</p>
      </div>
    </div>
  );
};

export default PopupRole;