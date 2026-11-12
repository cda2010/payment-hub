(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LogOperation', LogOperation);

    LogOperation.$inject = ['$resource', 'DateUtils'];

    function LogOperation ($resource, DateUtils) {
        var resourceUrl =  'api/log-operations/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateOperation = DateUtils.convertDateTimeFromServer(data.dateOperation);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
