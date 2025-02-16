"use client";

import { use } from "react";
import PolygonEditor from "@/components/polygons/PolygonEditor";

// Note: Here, params is now a Promise that resolves to an object with an id.
export default function Page({
                                 params,
                             }: {
    params: Promise<{ id: string }>;
}) {
    // Unwrap the promise to get the actual params object.
    const { id } = use(params);

    return <PolygonEditor polygonId={id} />;
}
