Cupa Application - Deployment Guide
====================================

This RPM package installs the Cupa application to /opt/cupa.

Installation:
1. Install the RPM: rpm -ivh cupa-*.rpm
2. Configure the application by editing /opt/cupa/application-prod.yml
3. Start the service: systemctl start cupa
4. Enable auto-start: systemctl enable cupa

Configuration:
- Main configuration: /opt/cupa/application-prod.yml
- Service file: /etc/systemd/system/cupa.service
- Logs: /var/log/cupa/cupa-YYYY-MM-DD.log

Default settings:
- Port: 8080
- Log level: DEBUG for lt.creditco.cupa
- Deployment directory: /opt/cupa
- User/Group: cupa

For proxy configuration, uncomment and configure the proxy settings in application-prod.yml.

Logs are rotated daily and stored in /var/log/cupa/ with format cupa-YYYY-MM-DD.log.
