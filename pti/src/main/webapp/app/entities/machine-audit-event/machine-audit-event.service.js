(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('MachineAuditEvent', MachineAuditEvent);

    MachineAuditEvent.$inject = ['$resource'];

    function MachineAuditEvent ($resource) {
        var resourceUrl =  'api/machine-audit-events/:id';

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
