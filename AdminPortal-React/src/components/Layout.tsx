import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";

/**
 * Port of the Angular app shell (AdminPortal/src/app/app.component.html):
 * `<app-navbar>` + `<router-outlet>` inside a `.container`.
 */
export default function Layout() {
  return (
    <div className="container">
      <Navbar />
      <Outlet />
    </div>
  );
}
