"use client";

import React from "react";
import PolygonEditor from "@/components/polygons/PolygonEditor";

interface PolygonEditPageProps {
    params: { id: string };
}

/**
 * This page at /polygons/[id]/edit loads the PolygonEditor component
 * to edit an existing polygon.
 */
export default function PolygonEditPage({ params }: PolygonEditPageProps) {
    const { id } = params;

    return (
        <div className="p-4">
            <PolygonEditor polygonId={id} />
        </div>
    );
}
