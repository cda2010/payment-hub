(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('WebserviceDetailController', WebserviceDetailController);

    WebserviceDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Webservice'];

    function WebserviceDetailController($scope, $rootScope, $stateParams, previousState, entity, Webservice) {
        var vm = this;

        vm.webservice = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:webserviceUpdate', function(event, result) {
            vm.webservice = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
