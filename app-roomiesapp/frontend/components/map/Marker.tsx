interface MarkerRoleProps {
    order: number;
}

const MarkerRole = ({ order }: MarkerRoleProps) => {
    return (
        <div className="group relative flex h-14 w-14 cursor-pointer items-center justify-center transition-transform hover:scale-110 active:scale-95">
            <div className="z-10 flex h-11 w-11 items-center justify-center rounded-full border-[3px] border-white bg-[#cc241a] text-white shadow-md">
                <span className="text-lg font-extrabold">{order}</span>
            </div>
            
            <div className="absolute bottom-1 -z-10 h-4 w-4 rotate-45 transform bg-[#cc241a]"></div>
        </div>
    );
};

export default MarkerRole;