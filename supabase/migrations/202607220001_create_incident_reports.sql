create table if not exists public.incident_reports (
    id uuid primary key,
    type text not null,
    severity text not null,
    status text not null default 'SUBMITTED',
    description text,
    latitude double precision not null,
    longitude double precision not null,
    location_precision text not null,
    occurred_at_millis bigint not null,
    visibility text not null,
    is_anonymous boolean not null default true,
    created_at timestamptz not null default now(),
    constraint incident_reports_type_not_blank check (length(trim(type)) > 0),
    constraint incident_reports_severity_not_blank check (length(trim(severity)) > 0),
    constraint incident_reports_latitude_valid check (latitude between -90 and 90),
    constraint incident_reports_longitude_valid check (longitude between -180 and 180),
    constraint incident_reports_visibility_valid check (visibility in ('COMMUNITY', 'PRIVATE'))
);

create index if not exists incident_reports_occurred_at_idx
on public.incident_reports (occurred_at_millis desc);

create index if not exists incident_reports_visibility_idx
on public.incident_reports (visibility);

alter table public.incident_reports enable row level security;

grant select, insert on table public.incident_reports to anon, authenticated;

drop policy if exists "public can create anonymous incident reports"
on public.incident_reports;
create policy "public can create anonymous incident reports"
on public.incident_reports
for insert
to anon, authenticated
with check (is_anonymous = true and visibility = 'COMMUNITY');

drop policy if exists "public can read community incident reports"
on public.incident_reports;
create policy "public can read community incident reports"
on public.incident_reports
for select
to anon, authenticated
using (visibility = 'COMMUNITY');
