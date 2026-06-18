export function ResultSkeleton() {
  return (
    <div className="flex items-center justify-between p-2 bg-gg-beige-extralight border border-gray-100 rounded-xl animate-pulse">
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 bg-gg-beige-light rounded-full"></div>
        <div className="flex flex-col gap-2">
          <div className="h-3 bg-gg-beige rounded w-24"></div>
          <div className="h-2 bg-gg-beige-light rounded w-16"></div>
        </div>
      </div>
      <div className="h-6 w-10 bg-gg-beige-light rounded-sm"></div>
    </div>
  )
}
