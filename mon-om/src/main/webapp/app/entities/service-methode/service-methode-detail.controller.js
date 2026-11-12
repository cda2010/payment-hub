(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('ServiceMethodeDetailController', ServiceMethodeDetailController);

    ServiceMethodeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ServiceMethode'];

    function ServiceMethodeDetailController($scope, $rootScope, $stateParams, previousState, entity, ServiceMethode) {
        var vm = this;

        vm.serviceMethode = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('omnicanalApp:serviceMethodeUpdate', function(event, result) {
            vm.serviceMethode = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
