(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('OperationAudit', OperationAudit);

    OperationAudit.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function OperationAudit ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'operation-audits/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.date = DateUtils.convertDateTimeFromServer(data.date);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
