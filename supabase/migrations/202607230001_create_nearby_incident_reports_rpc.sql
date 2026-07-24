create or replace function public.nearby_incident_reports(
    p_latitude double precision,
    p_longitude double precision,
    p_radius_meters integer default 1500,
    p_limit integer default 100
)
returns table (
    id uuid,
    report_id uuid,
    type text,
    severity text,
    status text,
    latitude double precision,
    longitude double precision,
    location_precision text,
    summary text,
    distance_meters integer,
    occurred_at_millis bigint
)
language sql
stable
security invoker
set search_path = public
as $$
    with distances as (
        select
            report.id,
            report.id as report_id,
            report.type,
            report.severity,
            report.status,
            report.latitude,
            report.longitude,
            report.location_precision,
            report.description as summary,
            round(
                6371000 * acos(
                    least(
                        1.0,
                        greatest(
                            -1.0,
                            cos(radians(p_latitude))
                                * cos(radians(report.latitude))
                                * cos(radians(report.longitude) - radians(p_longitude))
                                + sin(radians(p_latitude))
                                * sin(radians(report.latitude))
                        )
                    )
                )
            )::integer as distance_meters,
            report.occurred_at_millis
        from public.incident_reports as report
        where report.visibility = 'COMMUNITY'
    )
    select *
    from distances
    where distance_meters <= greatest(1, least(p_radius_meters, 50000))
    order by occurred_at_millis desc
    limit greatest(1, least(p_limit, 500));
$$;

grant execute on function public.nearby_incident_reports(
    double precision,
    double precision,
    integer,
    integer
) to anon, authenticated;
