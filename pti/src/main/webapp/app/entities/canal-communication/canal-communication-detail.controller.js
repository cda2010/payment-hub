(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalCommunicationDetailController', CanalCommunicationDetailController);

    CanalCommunicationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'CanalCommunication', 'Facturier', 'Ftp', 'RecepteurEmail'];

    function CanalCommunicationDetailController($scope, $rootScope, $stateParams, previousState, entity, CanalCommunication, Facturier, Ftp, RecepteurEmail) {
        var vm = this;

        vm.canalCommunication = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:canalCommunicationUpdate', function(event, result) {
            vm.canalCommunication = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
