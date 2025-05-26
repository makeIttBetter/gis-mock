"use client";

import React, {use} from "react";
import PolygonEditorContainer from "@/components/polygons/edit/PolygonEditorContainer";

export default function Page({
                                 params,
                             }: {
    params: Promise<{ id: string }>;
}) {
    const {id} = use(params);
    return <PolygonEditorContainer polygonId={id}/>;
}
