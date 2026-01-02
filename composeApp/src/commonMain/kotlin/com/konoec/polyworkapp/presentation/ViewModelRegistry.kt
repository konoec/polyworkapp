package com.konoec.polyworkapp.presentation

import com.konoec.polyworkapp.presentation.attendance.AttendanceViewModel
import com.konoec.polyworkapp.presentation.payments.PaymentsViewModel
import com.konoec.polyworkapp.presentation.schedule.ScheduleViewModel

/**
 * Registro centralizado de ViewModels para poder limpiar sus estados al hacer logout
 */
object ViewModelRegistry {
    private var attendanceViewModel: AttendanceViewModel? = null
    private var scheduleViewModel: ScheduleViewModel? = null
    private var paymentsViewModel: PaymentsViewModel? = null

    fun registerAttendanceViewModel(vm: AttendanceViewModel) {
        attendanceViewModel = vm
    }

    fun registerScheduleViewModel(vm: ScheduleViewModel) {
        scheduleViewModel = vm
    }

    fun registerPaymentsViewModel(vm: PaymentsViewModel) {
        paymentsViewModel = vm
    }

    /**
     * Limpia el estado de todos los ViewModels registrados.
     * Se debe llamar al hacer logout.
     */
    fun clearAllViewModels() {
        attendanceViewModel?.clearState()
        scheduleViewModel?.clearState()
        paymentsViewModel?.clearState()

        // Desregistrar ViewModels
        attendanceViewModel = null
        scheduleViewModel = null
        paymentsViewModel = null
    }
}

