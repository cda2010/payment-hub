(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VerifOtpDetailController', VerifOtpDetailController);

    VerifOtpDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'VerifOtp'];

    function VerifOtpDetailController($scope, $rootScope, $stateParams, previousState, entity, VerifOtp) {
        var vm = this;

        vm.verifOtp = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:verifOtpUpdate', function(event, result) {
            vm.verifOtp = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
