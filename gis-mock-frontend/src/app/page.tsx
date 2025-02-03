// frontend/src/app/page.tsx

"use client";
import React from "react";
import Link from "next/link";
import Image from "next/image";

export default function Home() {
    // Developer note: The order of technologies is defined here for clarity.
    // You can easily adjust or expand these arrays as your application grows.
    const devOpsTechnologies = [
        { name: "Docker", icon: "/docker.png" },
        { name: "AWS", icon: "/aws.png" },
    ];
    const backendTechnologies = [
        { name: "Python", icon: "/python.png" },
        { name: "FastAPI", icon: "/fast-api.png" },
    ];
    const frontendTechnologies = [
        { name: "React", icon: "/react.png" },
        { name: "Next.js", icon: "/next-js.png" },
    ];

    return (
        <main className="flex flex-col justify-center items-center min-h-screen p-8 bg-gray-100">
            {/* Title */}
            <h1 className="text-4xl sm:text-6xl font-bold mb-8 mt-[-5%] text-center">
                Financial Data Filtering App
            </h1>

            {/* Container for the lists of technologies */}
            <div className="flex flex-col gap-8 items-center w-full max-w-3xl">
                {/* DevOps Section */}
                <section className="w-full">
                    <h2 className="text-xl font-semibold mb-2">DevOps</h2>
                    <div className="flex flex-wrap gap-4">
                        {devOpsTechnologies.map((tech) => (
                            <div key={tech.name} className="flex flex-col items-center">
                                <Image
                                    src={tech.icon}
                                    alt={tech.name}
                                    width={100}
                                    height={100}
                                    className="mb-1"
                                />
                            </div>
                        ))}
                    </div>
                </section>

                {/* Backend Section */}
                <section className="w-full">
                    <h2 className="text-xl font-semibold mb-2">Backend</h2>
                    <div className="flex flex-wrap gap-4">
                        {backendTechnologies.map((tech) => (
                            <div key={tech.name} className="flex flex-col items-center">
                                <Image
                                    src={tech.icon}
                                    alt={tech.name}
                                    width={100}
                                    height={100}
                                    className="mb-1"
                                />
                            </div>
                        ))}
                    </div>
                </section>

                {/* Frontend Section */}
                <section className="w-full">
                    <h2 className="text-xl font-semibold mb-2">Frontend</h2>
                    <div className="flex flex-wrap gap-4">
                        {frontendTechnologies.map((tech) => (
                            <div key={tech.name} className="flex flex-col items-center">
                                <Image
                                    src={tech.icon}
                                    alt={tech.name}
                                    width={100}
                                    height={100}
                                    className="mb-1"
                                />
                            </div>
                        ))}
                    </div>
                </section>
            </div>

            {/* Button to go to /finance page */}
            <Link
                href="/finance"
                className="mt-12 px-6 py-3 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors text-lg"
            >
                Open Filter
            </Link>
        </main>
    );
}
