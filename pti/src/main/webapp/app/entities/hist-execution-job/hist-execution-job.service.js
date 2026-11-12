(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('HistExecutionJob', HistExecutionJob);

    HistExecutionJob.$inject = ['$resource', 'DateUtils'];

    function HistExecutionJob ($resource, DateUtils) {
        var resourceUrl =  'api/hist-execution-jobs/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.debutExecution = DateUtils.convertDateTimeFromServer(data.debutExecution);
                        data.finExecution = DateUtils.convertDateTimeFromServer(data.finExecution);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
