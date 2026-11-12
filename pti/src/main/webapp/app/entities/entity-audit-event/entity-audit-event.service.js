(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('EntityAuditEvent', EntityAuditEvent)
        .factory('EntityTypeEvent', EntityTypeEvent);

    EntityAuditEvent.$inject = ['$resource'];
    EntityTypeEvent.$inject = ['$resource','BASE_URL_API'];
    function EntityAuditEvent ($resource) {
        var resourceUrl =  'api/entity-audit-events/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryAll': { method: 'GET',url: 'api/entity-audit-events-optimiser', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
    function EntityTypeEvent ($resource,BASE_URL_API){
        var resourceUrl =  BASE_URL_API+'entity-audit-events';
        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }

        });
    }
})();
