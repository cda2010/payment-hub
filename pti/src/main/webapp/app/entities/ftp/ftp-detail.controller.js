(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FtpDetailController', FtpDetailController);

    FtpDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Ftp'];

    function FtpDetailController($scope, $rootScope, $stateParams, previousState, entity, Ftp) {
        var vm = this;

        vm.ftp = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ftpUpdate', function(event, result) {
            vm.ftp = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
